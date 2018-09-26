<template>
  <v-layout justify-center row wrap>
    <v-flex xs12>
      <thesaurus-process/>
    </v-flex>
  </v-layout>
</template>

<script>
  import ThesaurusProcess from "./thesaurusProcess";
  export default {
    name: "thesaurusManage",
    components: {ThesaurusProcess},
  }
</script>

<style scoped>

</style>
