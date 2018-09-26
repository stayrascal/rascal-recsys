<template>
  <v-layout justify-center row wrap>
    <v-flex xs12 sm8 text-xs-center class="px-2" justify-center row wrap>
      <v-flex>
        <comp-search/>
      </v-flex>
      <v-flex class="py-2">
        <comp-import/>
      </v-flex>
    </v-flex>
    <v-flex xs12 sm4 text-xs-center class="px-2" justify-center row wrap>
      <comp-add/>
    </v-flex>
  </v-layout>
</template>

<script>
  import compAdd from "./compAdd"
  import CompImport from "./compImport";
  import CompSearch from "./compSearch";

  export default {
    name: "compManage",
    components: {CompSearch, CompImport, compAdd}
  }
</script>

<style scoped>

</style>
