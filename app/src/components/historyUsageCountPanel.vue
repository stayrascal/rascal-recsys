<template>
  <v-card flat>
    <v-card-title>
      <h3>用户最常使用的构件对，前{{num}}</h3>
    </v-card-title>
    <v-card-text>
      <v-slider v-model="num" thumb-label step="1" ticks min="1" max="30"/>
      <polar-area-chart :chart-data="datacollection"/>
    </v-card-text>
  </v-card>
</template>

<script>
  import PolarAreaChart from "../charts/PolarAreaChart";
  import {HISTORY} from "../configs/srapp.api";
  import {randomColor} from "../commons/tools";
  import _ from 'lodash'

  export default {
    components: {PolarAreaChart},
    name: "history-usage-count-panel",
    data: () => ({
      num: 10,
      datacollection: {labels: [], datasets: []}
    }),
    mounted() {
      this.getDataCollection()
    },
    methods: {
      getDataCollection() {
        this.$http.get(HISTORY + "/popular/count?limit=" + this.num)
          .then(response => {
            response.json().then(result => {
              let labels = result['totalFreqs']
                .map(record => record["compName"] + "=>" + record["followCompName"]);
              let data = result['totalFreqs']
                .map(record => record['totalFreq']);
              let colors = randomColor(result['numFound']);
              this.datacollection = {
                labels: labels,
                datasets: [{
                  label: '使用频率',
                  data: data,
                  backgroundColor: colors.backgroundColor,
                  borderColor: colors.borderColor,
                  borderWidth: 0.24
                }]
              }
            })
          })
      },
      updateDataset: _.debounce(function () {
        this.getDataCollection()
      }, 600)
    },
    watch: {
      num(newVal, oldVal) {
        this.updateDataset()
      }
    }
  }
</script>

<style scoped>

</style>
