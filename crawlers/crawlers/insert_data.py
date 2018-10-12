# -*- coding: utf-8 -*-
import math
from collections import Counter
from heapq import nlargest
from itertools import product, count

import jieba
import requests
import json
import numpy as np
from gensim.models import word2vec

from HBaseConnector import HBaseConnector

model = word2vec.Word2Vec.load("./baike_26g_news_13g_novel_229g.model")
np.seterr(all='warn')


def cosine_similarity(vec1, vec2):
    tx = np.array(vec1)
    ty = np.array(vec2)
    cos1 = np.sum(tx * ty)
    cost21 = np.sqrt(sum(tx ** 2))
    cost22 = np.sqrt(sum(ty ** 2))
    cosine_value = cos1 / float(cost21 * cost22)
    return cosine_value


def compute_similarity_by_avg(sents_1, sents_2):
    if len(sents_1) == 0 or len(sents_2) == 0:
        return 0.0

    vec1 = model[sents_1[0]]
    for word1 in sents_1[1:]:
        vec1 = vec1 + model[word1]

    vec2 = model[sents_2[0]]
    for word2 in sents_2[1:]:
        vec1 = vec1 + model[word2]

    similarity = cosine_similarity(vec1 / len(sents_1), vec2 / len(sents_2))
    return similarity


def cut_sentences(sentence):
    puns = frozenset(u'。！？')
    tmp = []
    for ch in sentence:
        tmp.append(ch)
        if puns.__contains__(ch):
            yield ''.join(tmp)
            tmp = []
    yield ''.join(tmp)


def create_stopwords():
    stop_list = [line.strip() for line in open("stopwords.txt", 'r', encoding='utf-8').readlines()]
    return stop_list


def sentences_similarity(sents_1, sents_2):
    counter = 0
    for sent in sents_1:
        if sent in sents_2:
            counter += 1
    return counter / (math.log(len(sents_1) + len(sents_2)))


def create_graph(word_sent):
    """
    Get similarity graph of sentences
    """
    num = len(word_sent)
    board = [[0.0 for _ in range(num)] for _ in range(num)]
    for i, j in product(range(num), repeat=2):
        if i != j:
            board[i][j] = compute_similarity_by_avg(word_sent[i], word_sent[j])
    return board


def calculate_score(weight_graph, scores, i):
    '''
    Calculate the score of sentence in graph
    '''
    length = len(weight_graph)
    d = 0.85
    added_score = 0.0

    for j in range(length):
        fraction = 0.0
        denominator = 0.0
        fraction = weight_graph[j][i] * scores[j]
        for k in range(length):
            denominator += weight_graph[j][k]
            if denominator == 0:
                denominator = 1
        added_score += fraction / denominator
    weighted_score = (1 - d) + d * added_score
    return weighted_score


def weight_sentences_rank(weight_graph):
    # set initial score with 0.5
    scores = [0.5 for _ in range(len(weight_graph))]
    old_scores = [0.0 for _ in range(len(weight_graph))]

    while different(scores, old_scores):
        for i in range(len(weight_graph)):
            old_scores[i] = scores[i]
        for i in range(len(weight_graph)):
            scores[i] = calculate_score(weight_graph, scores, i)
    return scores


def different(scores, old_scores):
    flag = False
    for i in range(len(scores)):
        if math.fabs(scores[i] - old_scores[i]) >= 0.0001:
            flag = True
            break
    return flag


def filter_symbols(sents):
    stopwords = create_stopwords() + ['。', ' ', '.', '：']
    _sents = []
    for sentence in sents:
        _sentence = []
        for word in sentence:
            if word not in stopwords:
                _sentence.append(word)
        if _sentence:
            _sents.append(_sentence)
    return _sents


def filter_model(sents):
    _sents = []
    for sentence in sents:
        _sentence = []
        for word in sentence:
            if word in model:
                _sentence.append(word)
        if _sentence:
            _sents.append(_sentence)
    return _sents


def summarize(text, n):
    tokens = cut_sentences(text)
    sentences = []
    sents = []
    for sent in tokens:
        sentences.append(sent)
        sents.append([word for word in jieba.cut(sent) if word])
    sents = filter_symbols(sents)
    sents = filter_model(sents)
    graph = create_graph(sents)

    scores = weight_sentences_rank(graph)
    sent_selected = nlargest(n, zip(scores, count()))
    sent_index = []
    for i in range(n):
        if len(sent_selected) > 0:
            sent_index.append(sent_selected[i][1])
    return [sentences[i] for i in sent_index]


def predict_proba(oword, iword):
    '''
    Calculate the transfer probability p(wk|wi)
    :param oword:
    :param iword:
    :return:
    '''
    iword_vec = model[iword]
    oword = model.wv.vocab[oword]
    oword_l = model.trainables.syn1[oword.point].T
    dot = np.dot(iword_vec, oword_l)
    lprod = -sum(np.logaddexp(0, -dot) + oword.code * dot)
    return lprod


def get_keywords(sentence):
    s = [w for w in sentence if w in model]
    sentence_weight = {w: sum([predict_proba(u, w) for u in s]) for w in s}
    return list(map(lambda x: x[0], Counter(sentence_weight).most_common(10)))


add_item_url = 'http://localhost:9090/api/v1/items'


def add_item(data):
    response = requests.post(add_item_url, json=data)
    print(response.status_code)


if __name__ == '__main__':
    print("Starting............")
    connector = HBaseConnector()
    with connector.pool.connection() as connection:
        print("Connecting............")
        web_table = connection.table('BLOG')
        for key, data in web_table.scan():
            rowKey = int(key.decode('utf-8'))
            if rowKey > 3540244584512872:
                content = data[b'info:content'].decode('utf-8')

                item = {}
                item['uuid'] = key.decode('utf-8')
                item['title'] = data[b'info:title'].decode('utf-8')
                keywords = get_keywords(jieba.cut(content))
                item['tags'] = ','.join(keywords) + ',' + data[b'info:tag'].decode('utf-8')
                item['link'] = data[b'info:url'].decode('utf-8')
                print(content)
                summary = summarize(content, 1)
                item['describe'] = summary[0] if len(summary) > 0 else ''
                item['content'] = ''
                print(item)
                add_item(item)

# if __name__ == '__main__':
#     item = {}
#     item['uuid'] = '1029013241869253'
#     item['title'] = '谈谈命名'
#     item['tags'] = '王若行,注释,编程,源代码,规范,架构,谭浩强,编码,基本,引用, 新兴技术'
#     item['link'] = 'https://insights.thoughtworks.cn/talk-about-naming/'
#     item['describe'] = '首先注释不能跟着所有的引用，在定义处了解了d的含义，继续往下看的话却很容易忘记；其次代码更新了，很可能会忘记修改注释，反而给把读者带入歧途。'
#     item['content'] = ''
#     data = json.dumps(item)
#     print(data)
#     response = requests.post(add_item_url, json=item)
#     print(response)
