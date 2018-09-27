from scrapy.linkextractor import LinkExtractor
from scrapy.spiders import Rule

from crawlers.BaseSpider import BaseSpider
from crawlers.UrlManager import UrlManager

urlManager = UrlManager()


class BlogSpider(BaseSpider):
    name = "blog"
    rules = (
        Rule(LinkExtractor(allow=(urlManager.first_entry_matcher), process_links="process_links")),
        Rule(LinkExtractor(allow=urlManager.start_urls), callback="process_item")
    )
