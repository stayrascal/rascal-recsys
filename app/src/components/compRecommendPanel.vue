<template>
  <v-card flat>
    <v-card-text>
      <h3>为正在使用构件“{{compName}}”的用户“{{uuid}}”推荐以下至多{{num}}个构件</h3>
    </v-card-text>
    <v-card-text>
      <v-slider v-model="num" thumb-label step="1" ticks min="1" max="30"/>
      <comp-recommend-chart :chart-data="recommendList" :options="options"/>
    </v-card-text>
  </v-card>
</template>

<script>
  import CompRecommendChart from '../charts/BarChart'
  import {RECOMMEND} from '../configs/srapp.api'
  import {randomColor} from "../commons/tools";
  import _ from 'lodash'

  export default {
    name: "compRecommendPanel",
    components: {CompRecommendChart},
    mounted() {
      this.getRecommendList()
    },
    data() {
      return {
        num: 10,
        uuid: '',
        compName: '',
        recommendList: {labels: [], datasets: []},
        options: {
          onClick: this.selectComp
        }
      }
    },
    methods: {
      selectComp(event, eles) {
        this.compName = this.recommendList.labels[eles[0]._index];
        this.$router.push(`/recommend/${this.uuid}/${this.compName}`);
      },
      getRecommendList(uuid, compName) {
        if (uuid != null && compName != null) {
          this.uuid = uuid;
          this.compName = compName;
          this.$http.get(RECOMMEND + "?uuid=" + uuid + "&compName=" + compName + "&num=" + this.num)
            .then(response => {
              response.json().then(result => {
                let numFound = result['numFound'];
                if (numFound === 0) {
                  this.recommendList = {labels: [], datasets: []}
                } else {

                  let labels = result['prediction']
                    .map(record => record['followCompName']);
                  let predictions = result['prediction']
                    .map(record => record['prediction']);

                  let colors = randomColor(numFound);
                  this.recommendList = {
                    labels: labels,
                    datasets: [{
                      label: '# 预测频率',
                      data: predictions,
                      backgroundColor: colors.backgroundColor,
                      borderColor: colors.borderColor,
                      borderWidth: 1
                    }]
                  }
                }
              })
            })
        }
      },
      refreshList: _.debounce(function () {
        this.getRecommendList(this.uuid, this.compName)
      },500)
    },
    watch: {
      num(newVal, oldVal) {
        this.refreshList()
      },
      '$route' (to, from) {
        // 对路由变化作出响应...
        this.uuid = to.params.uuid;
        this.compName = to.params.compName;
        this.refreshList()
      }
    }
  }
</script>

<style scoped>

</style>
