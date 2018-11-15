from pyltp import Segmentor
from pyltp import SentenceSplitter
from gensim.summarization.summarizer import summarize

_segmentor = None
_sent_splitter = None

def split(content):
    global _segmentor, _sent_splitter
    if _segmentor is None:
        model_path = r''
        segmentor = Segmentor()
        segmentor.load(model_path)
        _segmentor = segmentor
        _sent_splitter = SentenceSplitter()
    sents = _sent_splitter.split(content)
    _sents = []
    for sent in sents:
        words = _segmentor.segment(sent)
        sent = ' '.join(words)
        _sents.append(sent)
    content = '. '.join(_sents)
    return content

def clean(content):
    content = content.replace('.', '')
    content = content.replace(' ', '')
    return content

if __name__ == '__main__':
    content = '''
    使用51篇文章的Opinion数据集进行比较。
    每篇文章都是关于产品的功能，例如iPod的电池寿命等，并且是购买该产品的客户的评论集合。
    数据集中的每篇文章都有5个手动编写的“黄金”摘要。
    通常5金总结是不同的，但它们也可以是相同的文本重复5次。
    LexRank是这里的赢家，因为它产生了更好的ROUGE和BLEU分数。
    不幸的是，我们发现由Gensim的TextRank和Luhn模型产生的摘要信息比摘要要少。
    此外，LexRank并不总是在ROUGE评分中击败TextRank  - 例如，TextRank在DUC 2002数据集上的表现稍好于LexRank。
    因此，LexRank和TextRank之间的选择取决于您的数据集，值得一试。
    数据的另一个结论是Gensim的Textrank优于普通的PyTextRank。
    因为它在明文TextRank中使用BM25函数而不是余弦IDF。
    表中的另一点是Luhn的算法具有较低的BLEU分数。
    这是因为它提取了更长的摘要，因此涵盖了更多的产品评论。
    不幸的是，我们不能缩短它，因为Sumy中Luhn算法的封装不提供参数来改变字数限制。
    '''
    tokens = split(content)
    result = summarize(tokens)
    result = clean(result)
    print(result)
