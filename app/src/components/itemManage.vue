<template>
  <v-layout justify-center row wrap>
    <v-flex xs12 sm16 text-xs-center class="px-2" justify-center row wrap>
      <v-flex>
        <item-search/>
      </v-flex>
      <!--<v-flex class="py-2">
        <item-import/>
      </v-flex>-->
    </v-flex>
    <!--<v-flex xs12 sm4 text-xs-center class="px-2" justify-center row wrap>
      <item-add/>
    </v-flex>-->
  </v-layout>
</template>

<script>
  import ItemSearch from './itemSearch';
  import ItemImport from './itemImport';
  import ItemAdd from './itemAdd';

  export default {
    name: "itemManage",
    components: { ItemAdd, ItemImport, ItemSearch }
  }
</script>

<style scoped>

</style>
