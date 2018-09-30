from scrapy.exceptions import DropItem


class DuplicatesPipeline(object):
    def __init__(self):
        self.links = set()

    def process_item(self, item, spider):
        if item['link'] in self.links:
            raise DropItem('Duplicate item found: %s' % item['title'])
        else:
            self.links.add(item['link'])
            return item


class SavePipeline(object):
    def __init__(self):
        self.saver = object
        self.web_pages_cached = []

    def process_item(self, item, spider):

        if len(self.web_pages_cached) >= 100:
            # self.hdb.save(self.web_pages_cached)
            map(print, self.web_pages_cached)
            self.web_pages_cached = []
        else:
            self.web_pages_cached.append(item)
