from scrapy.linkextractors import LinkExtractor
from scrapy.spiders import Rule

from crawlers.BaseSpider import BaseSpider
from crawlers.UrlManager import UrlManager

urlManager = UrlManager()


class BlogSpider(BaseSpider):
    name = "blog"
    rules = (
        Rule(link_extractor=LinkExtractor(restrict_css=('widget_categories'))),
        Rule(link_extractor=LinkExtractor(allow=urlManager.article_matcher),
             follow=False,
             callback="process_item")
    )
