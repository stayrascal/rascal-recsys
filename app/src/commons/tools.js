export function randomColor(num) {
  let bgColors = [];
  let bdColors = [];

  for (let i = 0; i < num; i++) {
    let r = Math.floor(Math.random() * 256);
    let g = Math.floor(Math.random() * 256);
    let b = Math.floor(Math.random() * 256);
    bgColors.push(`rgba(${r}, ${g}, ${b}, 0.2)`);
    bdColors.push(`rgba(${r}, ${g}, ${b}, 1)`);
  }
  return {
    backgroundColor: bgColors,
    borderColor: bdColors
  }
}
