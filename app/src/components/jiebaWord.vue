<template>
  <v-card class="elevation-3">
    <v-card-title>
      <h3>单词比重</h3>
    </v-card-title>
    <v-container fluid>
      <v-layout justify-center>
        <v-flex xs4 >
          <p class="text-xs-center">单词</p>
        </v-flex>
        <v-flex xs4 >
          <p class="text-xs-center">比重</p>
        </v-flex>
        <v-flex xs4 >
          <p class="text-xs-center">词性</p>
        </v-flex>
      </v-layout>
      <v-layout justify-center align-center row wrap>
        <v-flex xs4 >
          <p class="text-xs-center show">{{name}}</p>
        </v-flex>
        <v-flex xs4 >
          <p class="text-xs-center show highlight">{{weight.toFixed(0)}}</p>
        </v-flex>
        <v-flex xs4 >
          <p class="text-xs-center show">{{tag}}</p>
        </v-flex>
      </v-layout>
    </v-container>
  </v-card>
</template>

<script>
  export default {
    name: "jiebaWord",
    props: {
      name: '',
      weight: 0,
      tag: ''
    }
  }
</script>

<style scoped>
  .show {
    font-size: 1.3em;
  }
  .highlight {
    font-size: 2.2em;
  }
</style>
