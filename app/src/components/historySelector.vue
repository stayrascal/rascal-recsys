<template>
  <v-card flat>
    <v-card-title>
      <h3>{{title}}</h3>
    </v-card-title>
    <v-card-text>
      <v-form v-model="validate">
        <v-layout :justify-start="!horizon" :justify-center="horizon" row wrap>
          <v-flex xs12 :sm4="horizon">
            <v-text-field v-model="uuid" label="用户UUID" prepend-icon="perm_identity" :rules="uuidRules">
            </v-text-field>
          </v-flex>
          <v-flex xs12 :sm4="horizon" :class="{'py-3': !horizon}">
            <v-select
              :loading="isChecking"
              :items="components"
              :search-input.sync="search"
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
          <v-flex xs12 :sm4="horizon" :class="{'py-3': !horizon}">
            <v-btn color="primary" @click="sendSignal" :disabled="!validate">{{actionText}}</v-btn>
          </v-flex>
        </v-layout>
      </v-form>
    </v-card-text>
  </v-card>

</template>

<script>
  import {COMP} from "../configs/srapp.api";
  import _ from 'lodash'

  export default {
    name: "historySelector",
    props: {
      title: '',
      actionText: '',
      horizon: false
    },
    data: () => ({
      validate: false,
      addable: false,
      uuid: '',
      compName: null,
      search: '',
      components: [],
      isChecking: false,
      uuidRules: [
        v => !!v || 'uuid 不能为空',
        v => /^[0-9]+$/.test(v) || 'uuid 需满足数字格式'
      ],
      compNameRules: [
        v => !!v || '构件名不能为空',
        v => /^[A-Z]+\.[A-Za-z_]+$/.test(v) || '构件名格式如：(ARRAYUTIL.join)'
      ]
    }),
    methods: {

      sendSignal() {
        let validateCount = 0;
        if (this.isChecking) {
          validateCount += 1;
          this.$message.info("正在检查中，请稍后！")
        }

        if (validateCount === 0) {
          this.$emit("selected", {uuid: this.uuid, compName: this.compName})
        }
      },
      reloadComponents(search) {
        if (!(!!search)) return;
        let query = search.trim();
        this.isChecking = true;
        this.$http.get(COMP + '/' + query)
          .then(response => {
            response.json().then(result => {
              let size = result['comps'].length;
              this.components = result['comps'].map(comp => ({text: comp.name}));
              this.isChecking = false;
            }, error => {
              this.isChecking = false;
              this.$message.error("发生不可知的错误，请联系管理员!")
            })
          })
      },
      reloadComponentsWait: _.debounce(function () {
        this.reloadComponents(this.search)
      }, 600)
    },

    watch: {
      search(newVal, oldVal) {
        if (!(!!newVal)) {
          this.components = [];
          this.isChecking = false;
        } else {
          this.reloadComponentsWait()
        }
      }
    }
  }
</script>

<style scoped>
  .radius {
    border-radius: 5px;
    border: 1px;
  }
</style>
