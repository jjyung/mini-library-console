<script setup lang="ts">
defineProps<{
  code: string
  text: string
}>()
</script>

<template>
  <section data-testid="result-panel">
    <strong data-testid="result-code">{{ code }}</strong>
    <p data-testid="result-message">{{ text }}</p>
  </section>
</template>
