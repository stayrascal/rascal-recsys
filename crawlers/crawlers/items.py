import scrapy


class BlogItem(scrapy.Item):
    title = scrapy.Field()
    content = scrapy.Field()
    url = scrapy.Field()
    tag = scrapy.Field()
    spider = scrapy.Field()
    date = scrapy.Field()
