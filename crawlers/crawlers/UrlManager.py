class UrlManager(object):
    start_urls = ["https://insights.thoughtworks.cn/"]

    category_matcher = r"https://insights.thoughtworks.cn/category/*/$"
    article_matcher = r"https://insights.thoughtworks.cn/[a-z|-]+/$"
