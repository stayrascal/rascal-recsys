<template>
  <v-card>
    <v-card-title>
      <h1>构件推荐</h1>
    </v-card-title>
    <v-divider/>
    <v-card-text>
      <v-layout justify-center row wrap>
        <v-flex xs10 sm4 class="px-2 py-2">
          <history-selector title="输入用户uuid和正在使用的构件名"
                            actionText="推荐"
                            @selected="refreshTables" ref="selector"/>
        </v-flex>
        <v-spacer/>
        <v-flex xs12 sm8 class="px-2 py-2">
          <comp-recommend-panel ref="compRecommend"/>
        </v-flex>
      </v-layout>
    </v-card-text>
  </v-card>
</template>

<script>
  import HistorySelector from "./historySelector";
  import CompRecommendPanel from "./compRecommendPanel";

  export default {
    name: "historyManage",
    components: {
      CompRecommendPanel,
      HistorySelector},
    data: () => ({
      search: false
    }),
    methods: {
      refreshTables(data) {
        this.$refs.compRecommend.$router.push(`/recommend/${data.uuid}/${data.compName}`)
      }
    }
  }
</script>

<style scoped>

</style>
