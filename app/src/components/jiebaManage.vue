<template>
  <v-layout justify-center align-start row wrap>
    <v-flex xs12 sm10>
      <v-card>
        <v-card-title>
          <h1>查看语句划分结果</h1>
        </v-card-title>
        <v-divider/>
        <v-card-text>
          <jieba-cut ref="segmentor"/>
        </v-card-text>
      </v-card>
    </v-flex>
    <v-flex xs12 sm10 class="py-2">
      <v-card>
        <v-card-title>
          <h1>单词比重调整</h1>
        </v-card-title>
        <v-divider/>
        <v-card-text>
          <v-layout row wrap>
            <v-flex xs12 sm5 class="px-2">
              <jieba-word v-bind="word"/>
            </v-flex>
            <v-spacer/>
            <v-flex xs12 sm7 class="px-2">
              <jieba-dict @change="refresh"/>
            </v-flex>
          </v-layout>
        </v-card-text>
      </v-card>
    </v-flex>

  </v-layout>
</template>

<script>
  import JiebaCut from "./jiebaCut";
  import JiebaDict from "./jiebaDict";
  import JiebaWord from "./jiebaWord";
  import {SEGMENT} from "../configs/srapp.api"
  import {TweenLite} from "gsap"

  export default {
    name: "jiebaManage",
    components: {JiebaWord, JiebaDict, JiebaCut},
    data: ()=>({
      word: {
        name:'',
        weight: 0,
        tag: ''
      }
    }),
    methods: {
      refresh(payload) {
        this.$http.get(SEGMENT + '/' + payload['word'])
          .then(response => response.json().then(result => {
            if (result['numFound'] === 1) {
              let wd = result['words'][0];
              this.word.name = wd.name;
              this.word.tag = wd.tag;
              TweenLite.to(this.$data.word, 0.5, {weight: wd.weight})
            }
          }), errors => this.$notify.error("未知错误，请联系管理员!"));
        this.$refs['segmentor'].cut()
      }
    }
  }
</script>

<style scoped>

</style>
