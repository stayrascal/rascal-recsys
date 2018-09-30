from scrapy.linkextractor import LinkExtractor
from scrapy.spiders import Rule

from crawlers.BaseSpider import BaseSpider
from crawlers.UrlManager import UrlManager

urlManager = UrlManager()


class BlogSpider(BaseSpider):
    name = "blog"
    rules = (
        Rule(LinkExtractor(allow=urlManager.category_matcher), process_links="process_links"),
        Rule(LinkExtractor(allow=urlManager.article_matcher), callback="process_item")
    )
