<template>
  <v-card>
    <v-card-title primary-title>
      <h3>添加Item</h3>
    </v-card-title>
    <v-card-text>
      <v-form v-model="valid">
        <v-text-field
          label="Item title"
          v-model="item.title"
          :rules="titleRules"
          :counter="titleCharLen"
          required
        ></v-text-field>
        <v-text-field
          label="Item link"
          v-model="item.link"
        ></v-text-field>
        <v-text-field
          label="Item tag"
          v-model="item.tag"
        ></v-text-field>
        <v-text-field multi-line
                      label="Item description"
                      v-model="item.describe"
                      :rules="descRules"
                      required
        ></v-text-field>
        <v-text-field multi-line
                      label="Item content"
                      v-model="item.content"
        ></v-text-field>
      </v-form>
    </v-card-text>
    <v-card-actions>
      <v-spacer></v-spacer>
      <v-btn color="primary" :disabled="!valid" @click="addItem">添加</v-btn>
    </v-card-actions>
  </v-card>
</template>

<script>
  import { ITEM } from '../configs/srapp.api'

  export default {
    name: "itemAdd",
    data() {
      return {
        valid: false,
        item: {
          title: '',
          describe: '',
          content: '',
          link: '',
          tag: ''
        },
        titleCharLen: 100,
        titleRules: [
          v => !!v || 'Title cannot be empty!',
          v => v.length <= this.titleCharLen || 'The length of title is too long!'
        ],
        descRules: [
          v => !!v || 'Item description cannot be empty!'
        ]
      }
    },
    methods: {
      addItem() {
        this.$http.post(ITEM, this.item)
          .then(v => v.json().then(result => {
            if (result['numFound'] === 1) {
              this.$message.success("Item: " + this.item.title + "add succeed!")
            }
          }), error => {
            if (error.status === 409) {
              this.$message.error("Item is already exist!")
            }
          })
      }
    }
  }
</script>

<style scoped>

</style>
