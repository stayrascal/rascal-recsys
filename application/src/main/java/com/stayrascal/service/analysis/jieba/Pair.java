package com.stayrascal.service.analysis.jieba;

public class Pair<K> {
    public K key;
    public Double freq = 0.0;

    public Pair(K key, Double freq) {
        this.key = key;
        this.freq = freq;
    }

    public Pair(K key, Long freq) {
        this.key = key;
        this.freq = 1.0 * freq;
    }

    @Override
    public String toString() {
        return "Candidate [key=" + key + ", freq=" + freq + "]";
    }
}
