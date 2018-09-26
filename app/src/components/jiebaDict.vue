<template>
  <v-card class="elevation-3">
    <v-card-title>
      <h3>组合和分割单词</h3>
    </v-card-title>
    <v-container fluid>
      <v-layout row justify-center align-center wrap>
        <v-flex xs12 sm4>
          <v-text-field label="单词1" v-model="word1"
                        tabindex="1"
                        :rules="wordRules"
                        @input="validate"
                        ref="wordInput1"
                        :counter="2"
          />
        </v-flex>
        <v-flex xs12 sm2>
          <v-layout justify-center align-center wrap>
            <v-flex xs12>
              <v-layout row justify-center>
                <v-tooltip bottom>
                  <v-btn class="d-flex align-center" flat fab color="primary"
                         slot="activator"
                         :disabled="!valid"
                         @click="combineWords"
                  >
                    <v-flex>
                      <v-icon large>add</v-icon>
                    </v-flex>
                  </v-btn>
                  <span>组合</span>
                </v-tooltip>
              </v-layout>
            </v-flex>
            <v-flex xs12>
              <v-layout row justify-center>
                <v-tooltip bottom>
                  <v-btn class="d-flex align-center" flat fab color="primary"
                         slot="activator"
                         :disabled="!valid"
                         @click="separateWord"
                  >
                    <v-flex>
                      <v-icon large class="separate">remove</v-icon>
                    </v-flex>
                  </v-btn>
                  <span>分割</span>
                </v-tooltip>
              </v-layout>
            </v-flex>
          </v-layout>
        </v-flex>
        <v-flex xs12 sm4>
          <v-text-field label="单词2" v-model="word2"
                        tabindex="2"
                        :counter="2"
                        ref="wordInput2"
                        @input="validate"
                        :rules="wordRules"
                        auto-grow
          />
        </v-flex>
      </v-layout>
    </v-container>
  </v-card>
</template>

<script>
  import {SEGMENT} from "../configs/srapp.api";

  export default {
    name: "jiebaDict",
    data: () => ({
      valid: false,
      word1: '',
      word2: '',
      wordRules: [
        v => !!v || '单词不能为空',
        v => v.length < 3 || '字符不能超过2个'
      ]
    }),
    computed: {
      isCombine() {
        return this.action === 'combine'
      },
      word() {
        return this.word1.trim() + this.word2.trim();
      },
      words() {
        return this.word1.trim() + ' ' + this.word2.trim();
      }
    },
    methods: {
      validate() {
        this.valid = this.$refs['wordInput1'].validate(true)
          && this.$refs['wordInput2'].validate(true);

        if (this.valid) {
          this.emitChange()
        }
      },
      combineWords() {
        this.$http.post(SEGMENT + '?sentence=' + this.word + '&action=tune')
          .then(response => response.json().then(result => {
            this.emitChange(true)
          }), errors => this.$notify.error("发生未知错误，请联系管理员！"))
      },
      separateWord() {
        this.$http.post(SEGMENT + '?sentence=' + this.words + '&action=tune')
          .then(response => response.json().then(result => {
            this.emitChange(false)
          }), errors => this.$notify.error("发生未知错误，请联系管理员！"))
      },
      emitChange(isCombine) {
        this.$emit('change', {word: this.word, isCombine: isCombine})
      }
    }
  }
</script>

<style scoped>
  .word {
    display: inline-block;
    width: 30%;
  }

  .separate {
    -webkit-transform: rotate(-60deg);
    -moz-transform: rotate(-60deg);
    -ms-transform: rotate(-60deg);
    -o-transform: rotate(-60deg);
    transform: rotate(-60deg);
  }
</style>
