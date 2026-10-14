import { DestroyRef, Directive, ElementRef, OnInit, inject, input } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NgControl } from '@angular/forms';

import { TAMANHO_MAXIMO, TipoMascara, aplicarMascara } from '../utils/mascara.util';

@Directive({
  selector: '[appMascara]',
  host: {
    '(input)': 'aoDigitar($event)',
    '[attr.maxlength]': 'tamanhoMaximo()',
    '[attr.inputmode]': '"numeric"',
  },
})
export class MascaraDirective implements OnInit {
  readonly appMascara = input.required<TipoMascara>();

  private readonly elemento = inject<ElementRef<HTMLInputElement>>(ElementRef);
  private readonly destroyRef = inject(DestroyRef);
  private readonly ngControl = inject(NgControl, { optional: true });

  protected tamanhoMaximo(): number {
    return TAMANHO_MAXIMO[this.appMascara()];
  }

  ngOnInit(): void {
    const controle = this.ngControl?.control;

    controle?.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.normalizarValorExterno());

    this.normalizarValorExterno();
  }

  protected aoDigitar(evento: Event): void {
    const campo = evento.target as HTMLInputElement;
    const formatado = aplicarMascara(campo.value, this.appMascara());

    campo.value = formatado;
    this.ngControl?.control?.setValue(formatado, { emitEvent: false });
  }

  private normalizarValorExterno(): void {
    const controle = this.ngControl?.control;
    const valor = controle?.value;

    if (typeof valor !== 'string' || valor.length === 0) {
      return;
    }

    const formatado = aplicarMascara(valor, this.appMascara());

    if (formatado !== valor) {
      controle?.setValue(formatado, { emitEvent: false });
      this.elemento.nativeElement.value = formatado;
    }
  }
}
