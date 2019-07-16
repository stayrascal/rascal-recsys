from datetime import datetime
from csv import DictReader
from math import exp, log, sqrt
from random import random
import pickle

train = './train.csv'
test = './test.csv'

alpha = .005  # learning rate
beta = 1.  # smoothing parameter for adaptive learning rate
L1 = 0.  # L1 regularization, larger value means more regularized
L2 = 1.  # L2 regularization, larger value means more regularized

D = 2 ** 24  # number of weights to use
interaction = False  # whether to enable poly2 feature interactions

epoch = 1  # learn training data for N passes
holdafter = 9  # data after data N (exclusive) are used as validation
holdout = None  # use every N training instance for holdout validation


class ftrl_proximal(object):
    def __init__(self, alpha, beta, L1, L2, D, interaction):
        self.alpha = alpha
        self.beta = beta
        self.L1 = L1
        self.L2 = L2

        self.D = D
        self.interaction = interaction

        self.n = [0.] * D  # squared sum of past gradients
        self.z = [random() for k in range(D)]  # [0.] * D weights
        self.w = {}  # lazy weights

    def _indices(self, x):
        '''
        A helper generator that yields the indices in x
        The propose of this generator is to make the following code a bit cleaner when doing feature interaction.
        :param x:
        :return:
        '''

        # first yield index of the bias term
        yield 0

        # then yield the normal indices
        for index in x:
            yield index

        # now yield interactions (if applicable)
        if self.interaction:
            D = self.D
            L = len(x)

            x = sorted(x)
            for i in range(L):
                for j in range(i + 1, L):
                    # ont-hot encode interactions with hash trick
                    yield abs(hash(str(x[i]) + '_' + str(x[j]))) % D

    def predict(self, x):
        ''' Get probability estimation on x
        :param x:  features
        :return: probability of p(y=1|x;w)
        '''
        # parameters
        alpha = self.alpha
        beta = self.beta
        L1 = self.L1
        L2 = self.L2

        # model
        n = self.n
        z = self.z
        w = {}

        # wTx is the inner product of w and x
        wTx = 0.
        for i in self._indices(x):
            sign = -1. if z[i] < 0 else 1.  # get sign of z[i]

            # build w on the fly using z and n, hence the name - lazy weights
            # we are doing this at prediction instead of update time is because this allows us for not storing the complete w
            if sign * z[i] <= L1:
                # w[i] vanished due to L1 regularization
                w[i] = 0.
            else:
                # apply prediction time L1, L2 regularization to z and get w
                w[i] = (sign * L1 - z[i]) / ((beta + sqrt(n[i])) / alpha + L2)

            wTx += w[i]

        # cache the current w for update stage
        self.w = w

        # bounded sigmoid function, this is the probability estimation
        return 1. / (1. + exp(-max(min(wTx, 35.), -35.)))

    def update(self, x, p, y):
        ''' Update model using x, p, y
        :param x: feature, a list of indices
        :param p: click probability prediction of our model
        :param y: answer
        :return:

        MODIFIES:
            self.n: increase by squared gradient
            self.z: weights
        '''

        # parameter
        alpha = self.alpha

        # model
        n = self.n
        z = self.z
        w = self.w

        # gradient under logloss
        g = p - y

        # update z and n
        for i in self._indices(x):
            sigma = (sqrt(n[i] + g * g) - sqrt(n[i])) / alpha
            z[i] += g - sigma * w[i]
            n[i] += g * g


def logloss(p, y):
    p = max(min(p, 1. - 10e-15), 10e-15)
    return -log(p) if y == 1. else -log(1. - p)


def data(path, D):
    for t, row in enumerate(DictReader(open(path), delimiter=',')):
        try:
            ID = row['ID']
            del row['ID']
        except:
            pass

        y = 0.
        target = 'target'
        if target in row:
            if row[target] == '1':
                y = 1.
            del row[target]

        x = []
        for key in row:
            value = row[key]
            index = abs(hash(key + '_' + value)) % D
            x.append(index)

        yield ID, x, y


start = datetime.now()
learner = ftrl_proximal(alpha, beta, L1, L2, D, interaction)

for e in range(epoch):
    loss = 0.
    count = 0
    for t, x, y in data(train, D):
        p = learner.predict(x)
        loss += logloss(p, y)
        learner.update(x, p, y)
        count += 1
        if count % 1000 == 0:
            print('%s\tencountered: %d\tcurrent logloss: %f' % (datetime.now(), count, loss / count))
        if count > 10000:
            break
