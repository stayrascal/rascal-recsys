<template>
  <v-layout row wrap>
    <v-flex xs12>
      <history-selector
        title="用户uuid和使用的第一个构件名"
        action-text="查询历史"
        :horizon="true"
        @selected="fillHistory"
      />
    </v-flex>
    <v-flex xs12>
      <v-layout row wrap>
        <v-flex xs12 sm6>
          <v-card flat>
            <v-card-title>
              <h3>用户“{{uuid}}”使用构件“{{compName}}”的情况</h3>
            </v-card-title>
            <v-card-text>
              <bar-chart :chart-data="history"/>
            </v-card-text>
          </v-card>
        </v-flex>
        <v-flex xs12 sm6>
          <v-card flat>
            <v-card-title>
              <h3>使用构件“{{compName}}”后使用其他构件的人数</h3>
            </v-card-title>
            <v-card-text>
              <bar-chart :chart-data="usagePopulation"/>
            </v-card-text>
          </v-card>
        </v-flex>
      </v-layout>
    </v-flex>
  </v-layout>
</template>

<script>
  import HistorySelector from "./historySelector";
  import BarChart from "../charts/BarChart";
  import {HISTORY} from "../configs/srapp.api";
  import {randomColor} from "../commons/tools";

  export default {
    components: {
      BarChart,
      HistorySelector
    },
    name: "history-usage-for-user",
    data: () => ({
      uuid: '',
      compName: '',
      history: {labels: [], datasets: []},
      usagePopulation: {labels: [], datasets: []}
    }),
    methods: {
      fillHistory(data) {
        this.uuid = data.uuid;
        this.compName = data.compName;
        this.getHistory(data);
        this.getUsagePopulation(data)
      },
      getHistory(data) {
        this.$http.get(HISTORY, {
          params: {
            userName: data.uuid,
            compName: data.compName
          }
        }).then(response => {
          response.json().then(result => {
            console.log(result);
            let labels = result['history']
              .map(record => record['followCompName']);
            let freqs = result['history']
              .map(record => record['freq']);
            let colors = randomColor(result['numFound']);
            this.history = {
              labels: labels,
              datasets: [{
                label: `构件使用次数。`,
                data: freqs,
                backgroundColor: colors.backgroundColor,
                borderColor: colors.borderColor,
                borderWidth: 1
              }]
            }
          })
        })
      },
      getUsagePopulation(data) {
        this.$http.get(HISTORY + `/${data.compName}/quantity`)
          .then(response => {
            response.json().then(result => {
              console.log(result);
              let labels = result['numOfUsers']
                .map(record => record['followCompName']);
              let quantities = result['numOfUsers']
                .map(record => record['quantity']);
              let colors = randomColor(result['numFound']);
              this.usagePopulation = {
                labels: labels,
                datasets: [{
                  label: '使用人数',
                  data: quantities,
                  backgroundColor: colors.backgroundColor,
                  borderColor: colors.borderColor,
                  borderWidth: 1
                }]
              }
            })
          })
      },
      buildDataset(data) {
        let historyWithUser
      }
    }
  }
</script>

<style scoped>

</style>
