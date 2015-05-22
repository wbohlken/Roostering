__author__ = 'remco'

from os import listdir
from os.path import isfile, join

totals = {}
counts = {}
averages = {}


def analyze():
    files = [f for f in listdir('results') if isfile(join('results', f)) and f.startswith('hc_full_')]
    for file in files:
        get_total(file)
    for a in totals:
        averages[a] = totals[a] / counts[a]
    print averages


def get_total(file_name):
    metric = file_name[26:]
    if not totals.has_key(metric):
        totals[metric] = []
        counts[metric] = []
    with open(join('results', file_name)) as data:
        for line in data:
            int_values = map(int, line.split(','))
            if len(totals[metric]) == 0:
                totals[metric] = int_values
            else:
                if len(totals[metric]) < len(int_values):
                    for i, value in enumerate(int_values):
                        totals[metric][i] += value


        # totals[metric] += int(line[line.rindex(',')+1:].rstrip())
            # counts[metric] += 1


if __name__ == "__main__":
    analyze()