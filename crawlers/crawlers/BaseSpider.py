from scrapy.spiders import CrawlSpider
from scrapy.http import TextResponse
from crawlers.UrlManager import UrlManager
from crawlers.items import BlogItem

urlManager = UrlManager()


class BaseSpider(CrawlSpider):
    allowed_domains = ['insights.thoughtworks.cn']
    start_urls = urlManager.start_urls

    def parse_item(self, response):
        print("parse item url is :{0}".format(response.url))
        if isinstance(response, TextResponse):
            blog = BlogItem()
            blog['url'] = response.url
            blog['content'] = response.xpath('//article/text()').extract_first()
            blog['title'] = response.xpath('').extract_first()
            blog['tag'] = response.xpath('').extract_first()
            yield blog

    def process_link(self, links):
        for link in links:
            print("{0} link is :{1}".format(self.name, link.url))
            yield link
