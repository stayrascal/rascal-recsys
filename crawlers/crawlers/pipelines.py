from scrapy.exceptions import DropItem

from crawlers.HBaseConnector import HBaseConnector


class DuplicatesPipeline(object):
    def __init__(self):
        self.links = set()

    def process_item(self, item, spider):
        if item['url'] in self.links:
            raise DropItem('Duplicate item found: %s' % item['title'])
        else:
            self.links.add(item['url'])
            return item


class SavePipeline(object):
    def __init__(self):
        self.saver = HBaseConnector()
        self.web_pages_cached = []

    def process_item(self, item, spider):

        if len(self.web_pages_cached) >= 10:
            self.saver.insert_to_web_table(self.web_pages_cached)
            self.web_pages_cached = []
        else:
            self.web_pages_cached.append(item)
