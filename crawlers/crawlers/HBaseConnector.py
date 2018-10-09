import happybase as hb
from happybase import Batch
import time

CONSTANT_WEB_TABLE = b'BLOG'

dev_host = 'localhost'


class HBaseConnector(object):
    def __init__(self):
        self.pool = hb.ConnectionPool(size=4, host=dev_host, autoconnect=True, table_prefix='REC_SYS')

    def insert_to_web_table(self, webpages):
        with self.pool.connection() as connection:
            tables = connection.tables()
            if CONSTANT_WEB_TABLE not in tables:
                connection.create_table(CONSTANT_WEB_TABLE, families={'info': dict()})

            web_table = connection.table(CONSTANT_WEB_TABLE)
            current_timestamp = int(round(time.time() * 1000))
            with Batch(web_table, batch_size=1000, timestamp=current_timestamp) as web:
                for webpage in webpages:
                    row_key = str(abs(hash(webpage['url'])) % (10 ** 16))
                    web.put(row_key,
                            {
                                b'info:url': webpage['url'],
                                b'info:title': webpage['title'],
                                b'info:content': webpage['content'],
                                b'info:tag': webpage['tag'],
                                b'info:spider': webpage['spider'],
                                b'info:date': webpage['date']
                            })

    def scan_webpages(self):
        with self.pool.connection() as connection:
            tables = connection.tables()
            if CONSTANT_WEB_TABLE not in tables:
                connection.create_table(CONSTANT_WEB_TABLE, families={'info': dict()})

            web_table = connection.table(CONSTANT_WEB_TABLE)
            for key, data in web_table.scan():
                print('The RowKey is :', key)
                print('The content is :', data)
                yield key, data


if __name__ == '__main__':
    hbase = HBaseConnector()
    with hbase.pool.connection() as connection:
        tables = connection.tables()
        web_table = connection.table(CONSTANT_WEB_TABLE)
        for key, data in web_table.scan():
            print('The RowKey is :', key)
            print('The content is :', data['info:content'].decode('utf-8'))
    hbase.scan_webpages()
    # for key, data in hbase.scan_webpages():
    #     print('The RowKey is :{}' % key)
    #     print('The content is :{}' % data)
