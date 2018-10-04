import time
from scrapy.spiders import CrawlSpider
from scrapy.http import TextResponse
from crawlers.UrlManager import UrlManager
from crawlers.items import BlogItem
from scrapy.loader import ItemLoader

urlManager = UrlManager()


class BaseSpider(CrawlSpider):
    allowed_domains = ['insights.thoughtworks.cn']
    start_urls = urlManager.start_urls

    def process_item(self, response):
        print("parse item url is :{0}".format(response.url))
        if isinstance(response, TextResponse):
            # loader = ItemLoader(item=BlogItem(), response=response)
            # loader.add_css('url', 'site-main')
            blog = BlogItem()
            blog['url'] = response.url
            blog['content'] = response.css('article::text')
            # blog['content'] = response.css('.site-main::text').extract_first()
            blog['title'] = response.css('.entry-title::text').extract_first()
            blog['tag'] = response.css('.cat-links::text').extract_first()
            blog['spider'] = self.name
            blog['date'] = time.strftime("%W--%Y/%m/%d/--%H:%M:%S")
            print(blog)
            yield blog

    def process_links(self, links):
        print("The links is {}".format(links))
        for link in links:
            print("{0} link is :{1}".format(self.name, link.url))
            yield link
