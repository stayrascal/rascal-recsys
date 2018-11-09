import requests
import random

base_url = 'http://localhost:9090/api/v1/'
user_url = base_url + 'users'
item_url = base_url + 'items/all'
event_url = base_url + 'events'


def get_users():
    return requests.get(user_url).json()['users']


def get_items():
    return requests.get(item_url).json()['items']


def add_event(event):
    requests.post(event_url, json=event)


if __name__ == '__main__':
    users = list(map(lambda x: x['id'], get_users()))
    items = list(map(lambda x: x['id'], get_items()))

    for index in range(0, 100000):

        event = {}
        event['userId'] = random.choice(users)
        event['itemId'] = random.choice(items)
        event['action'] = 'VIEW'
        event['otherItems'] = ','.join(str(e) for e in random.sample(items, 5))
        if index % 1000 == 0:
            print(event)
        add_event(event)
