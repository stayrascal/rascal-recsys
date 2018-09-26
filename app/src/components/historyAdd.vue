<template>
  <v-layout justify-center>
    <v-flex xs10>
      <v-card class="elevation-3">
        <v-card-title>
          <h3>添加一条历史纪录</h3>
        </v-card-title>
        <v-card-text>
          <v-form v-model="validate">
            <v-layout justify-start row wrap>
              <v-flex xs12>
                <v-text-field v-model="uuid" label="用户UUID" prepend-icon="perm_identity" :rules="uuidRules">
                </v-text-field>
              </v-flex>
              <v-flex xs12 class="py-3">
                <v-select
                  :loading="isChecking"
                  :items="components"
                  :search-input.sync="searchCompName"
                  :rules="compNameRules"
                  v-model="compName"
                  label="构件名"
                  autocomplete
                  single-line
                  cache-items
                  item-value="text"
                  required
                  prepend-icon="extension"
                ></v-select>
              </v-flex>
              <v-flex xs12 class="py-3">
                <v-select
                  :loading="isChecking"
                  :items="followComponents"
                  :search-input.sync="searchFollowCompName"
                  :rules="compNameRules"
                  v-model="followCompName"
                  label="下一个构件名"
                  autocomplete
                  single-line
                  cache-items
                  item-value="text"
                  required
                  prepend-icon="extension"
                ></v-select>
              </v-flex>
              <v-flex xs12 class="py-3">
                <v-btn color="primary" :disabled="!validate || isChecking"
                       @click="addHistory">添加历史纪录</v-btn>
              </v-flex>
            </v-layout>
          </v-form>
        </v-card-text>
      </v-card>
    </v-flex>
  </v-layout>
</template>

<script>
  import {COMP} from "../configs/srapp.api";
  import {HISTORY} from "../configs/srapp.api";
  import _ from 'lodash'

  export default {
    name: "history-add",
    data: () => ({
      validate: false,
      uuid: '',
      compName: null,
      searchCompName: '',
      components: [],
      followCompName: null,
      searchFollowCompName: '',
      followComponents: [],
      isChecking: false,
      uuidRules: [
        v => !!v || 'uuid 不能为空',
        v => /^[0-9]+$/.test(v) || 'uuid 需满足数字格式'
      ],
      compNameRules: [
        v => !!v || '构件名不能为空',
        v => /^[A-Z]+\.[A-Za-z]+$/.test(v) || '构件名格式如：(ARRAYUTIL.join)'
      ]
    }),
    computed: {
      history() {
        return `${this.uuid},${this.compName},${this.followCompName}`
      }
    },
    methods: {
      reloadComponents(search, forComp) {
        if (!(!!search)) return;
        let query = search.trim();
        this.isChecking = true;
        this.$http.get(COMP + '/' + query)
          .then(response => {
            response.json().then(result => {
              let size = result['comps'].length;
              if (forComp) {
                this.components = result['comps'].map(comp => ({text: comp.name})).sort();
              } else {
                this.followComponents = result['comps'].map(comp => ({text: comp.name}));
              }
              this.isChecking = false;
            }, error => {
              this.isChecking = false;
              this.$message.error("发生不可知的错误，请联系管理员!")
            })
          })
      },
      reloadComponentsWait: _.debounce(function () {
        this.reloadComponents(this.searchCompName, true)
      }, 500),
      reloadFollowComponentsWait: _.debounce(function () {
        this.reloadComponents(this.searchFollowCompName, false)
      }, 500),
      addHistory() {
        let validateCount = 0;
        if (this.isChecking) {
          validateCount += 1;
          this.$message.info("正在检查中，请稍后！")
        }

        if (validateCount === 0) {
          this.$http.post(HISTORY + "?record=" + this.history).then(response => {
            response.json().then(result => {
              if (result["numFound"] === 1) {
                this.$message.success("历史纪录添加成功！")
              }
            })
          }, errors => {
            this.$message.error("添加历史纪录失败，请检查请求参数和网络状态。")
          });
          // console.log(this.history)
        }
      }
    },
    watch: {
      searchCompName(newVal, oldVal) {
        this.reloadComponentsWait()
      },
      searchFollowCompName(newVal, oldVal) {
        this.reloadFollowComponentsWait()
      }
    }
  }
</script>

<style scoped>

</style>
