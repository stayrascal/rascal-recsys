import argparse

from model import Model


def main():
    parser = argparse.ArgumentParser(description="Options")

    parser.add_argument('-dataName', action='store', dest='dataName', default='ml-1m')
    parser.add_argument('-negNum', action='store', dest='negNum', default=7, type=int)
    parser.add_argument('-userLayer', action='store', dest='userLayer', default=[512, 64])
    parser.add_argument('-itemLayer', action='store', dest='itemLayer', default=[1024, 64])
    # parser.add_argument('-reg', action='store', dest='reg', default=1e-3)
    parser.add_argument('-lr', action='store', dest='lr', default=0.0001)
    parser.add_argument('-maxEpochs', action='store', dest='maxEpochs', default=50, type=int)
    parser.add_argument('-batchSize', action='store', dest='batchSize', default=256, type=int)
    parser.add_argument('-earlyStop', action='store', dest='earlyStop', default=5)
    parser.add_argument('-checkPoint', action='store', dest='checkPoint', default='./checkPoint/')
    parser.add_argument('-topK', action='store', dest='topK', default=10)

    args = parser.parse_args()

    classifier = Model(args)

    classifier.run()


if __name__ == '__main__':
    main()
