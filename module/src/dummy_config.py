import json

config = {
    'wakeup_count': 0,
    'data': [],
    'commands': [],
    'received_cmd': [],
    'url': 'https://polypot.0xf00.ch/send-data/01234567-89ab-cdef-0123-456789abcdef',
    'wifi_param': {
        'ssid': 'AndroidAP',
        'password': 'glhv7012',
        'uuid': '01234567-89ab-cdef-0123-456789abcdef',
        'server': 'https://polypot.0xf00.ch'
    },
    'config': {
        'logging_interval': 10,
        'sending_interval': 10
    }
}

txt = json.dumps(config)
print(txt)
