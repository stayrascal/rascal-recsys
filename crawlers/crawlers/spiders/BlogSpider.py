from scrapy.linkextractors import LinkExtractor
from scrapy.spiders import Rule

from crawlers.BaseSpider import BaseSpider
from crawlers.UrlManager import UrlManager

urlManager = UrlManager()


class BlogSpider(BaseSpider):
    name = "blog"
    rules = (
        # Rule(link_extractor=LinkExtractor(allow=urlManager.category_matcher),
        #      callback="process_links"),
        Rule(LinkExtractor(restrict_xpaths='//a[@class="page-numbers"]')),
        Rule(link_extractor=LinkExtractor(allow=urlManager.article_matcher),
             follow=True,
             callback="process_item")
    )
