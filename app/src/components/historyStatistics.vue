<template>
    <v-layout row wrap>
      <v-flex xs12>
        <v-card>
          <v-card-title>
            <h1>用户历史使用情况</h1>
          </v-card-title>
          <v-divider/>
          <v-card-text>
            <history-usage-for-user class="py-2 px-2"/>
          </v-card-text>
        </v-card>
      </v-flex>
      <v-flex xs12 class="py-2">
        <v-card>
          <v-card-title>
            <h1>全体构件使用情况统计</h1>
          </v-card-title>
          <v-divider/>
          <v-card-text>
            <v-layout row wrap>
              <v-flex xs12 sm6 class="px-2">
                <history-usage-population-panel/>
              </v-flex>
              <v-flex xs12 sm6 class="px-2">
                <history-usage-count-panel/>
              </v-flex>
            </v-layout>
          </v-card-text>
        </v-card>
      </v-flex>
    </v-layout>
</template>

<script>
    import HistoryUsagePopulationPanel from "./historyUsagePopulationPanel";
    import HistoryUsageCountPanel from "./historyUsageCountPanel";
    import HistoryUsageForUser from "./historyUsageForUserPanel";

    export default {
      components: {
        HistoryUsageForUser,
        HistoryUsageCountPanel,
        HistoryUsagePopulationPanel},
      name: "history-statistics"
    }
</script>

<style scoped>

</style>
