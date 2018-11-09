import requests
import random

base_url = 'http://localhost:9090/api/v1/'
user_url = base_url + 'users'
item_url = base_url + 'items/all'
event_url = base_url + 'events'


def get_items():
    return requests.get(item_url).json()['items']


if __name__ == '__main__':
    items = list(map(lambda x: x['id'], get_items()))
    feature_columns = {
        'A': 3,
        'B': 10,
        'C': 5,
        'D': 5,
        'E': 100
    }




