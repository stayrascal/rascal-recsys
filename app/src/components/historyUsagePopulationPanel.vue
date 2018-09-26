<template>
  <v-card flat>
    <v-card-title>
      <h3>拥有最多使用人数的构件对, 前{{num}}</h3>
    </v-card-title>
    <v-card-text>
      <v-slider v-model="num" thumb-label step="1" ticks min="1" max="30"/>
      <polar-area-chart
        :chart-data="datacollection"
        :options="options"
      />
    </v-card-text>
  </v-card>
</template>

<script>
  import {HISTORY} from "../configs/srapp.api";
  import PolarAreaChart from "../charts/PolarAreaChart";
  import {randomColor} from '../commons/tools'
  import _ from 'lodash'

  export default {
    components: {PolarAreaChart},
    name: "history-usage-population-panel",
    data: () => ({
      datacollection: {labels: [], datasets: []},
      options: {},
      num: 10
    }),
    mounted() {
      this.getUsagePopulation();
    },
    methods: {
      getUsagePopulation() {
        this.$http.get(HISTORY + '/popular/population?limit=' + this.num)
          .then(response => {
            response.json().then(result => {
              let labels = result['numOfUsers']
                .map(record => record['compName'] + '=>' + record['followCompName']);
              let data = result['numOfUsers']
                .map(record => record['quantity']);

              let colors = randomColor(result['numFound']);
              this.datacollection = {
                labels: labels,
                datasets: [{
                  label: '构件使用人数',
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
        this.getUsagePopulation()
      }, 600)
    },
    watch: {
      num(newVal, oldVal) {
        this.updateDataset();
      }
    }
  }
</script>

<style scoped>

</style>
