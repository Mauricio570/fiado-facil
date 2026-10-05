/**
 * Cores com significado fixo em todos os gráficos dos relatórios: verde é o
 * que já foi pago, azul o que ainda vai vencer e vermelho o que está atrasado.
 *
 * O trio foi validado para daltonismo (protanopia e deuteranopia) e para
 * contraste sobre o fundo branco comparando todos os pares entre si — por isso
 * também é seguro na rosca, onde o primeiro e o último segmento se encostam.
 * Trocar uma dessas cores exige validar o conjunto de novo.
 */
export const COR_PAGO = '#008562';
export const COR_A_VENCER = '#3b6fd4';
export const COR_EM_ATRASO = '#cf1e1e';
