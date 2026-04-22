import Decimal from 'decimal.js';

Decimal.set({ precision: 19, scale: 4, rounding: Decimal.ROUND_HALF_UP });

export class Money {
  private constructor(private readonly value: Decimal) {}

  static fromNumber(amount: number): Money {
    return new Money(new Decimal(amount));
  }

  static fromString(amount: string): Money {
    return new Money(new Decimal(amount));
  }

  static zero(): Money {
    return new Money(new Decimal(0));
  }

  get raw(): number {
    return this.value.toNumber();
  }

  get cents(): number {
    return this.value.times(100).round().toNumber();
  }

  add(other: Money): Money {
    return new Money(this.value.plus(other.value));
  }

  subtract(other: Money): Money {
    return new Money(this.value.minus(other.value));
  }

  multiply(factor: number): Money {
    return new Money(this.value.times(factor));
  }

  divide(divisor: number): Money {
    return new Money(this.value.dividedBy(divisor));
  }

  isGreaterThan(other: Money): boolean {
    return this.value.greaterThan(other.value);
  }

  isLessThan(other: Money): boolean {
    return this.value.lessThan(other.value);
  }

  isZero(): boolean {
    return this.value.isZero();
  }

  format(locale = 'es-VE', currency = 'VES'): string {
    return new Intl.NumberFormat(locale, {
      style: 'currency',
      currency,
    }).format(this.value.toNumber());
  }

  toString(): string {
    return this.value.toFixed(4);
  }
}

export function parseMoney(value: string | number): Money {
  if (typeof value === 'number') {
    return Money.fromNumber(value);
  }
  return Money.fromString(value.replace(/[^\d.,]/g, '').replace(',', '.'));
}
