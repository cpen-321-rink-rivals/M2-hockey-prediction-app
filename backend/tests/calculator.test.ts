import { describe, expect, test, beforeEach, afterEach } from '@jest/globals';

// Example of testing a simple calculator module
class Calculator {
  private history: string[] = [];

  add(a: number, b: number): number {
    const result = a + b;
    this.history.push(`${a} + ${b} = ${result}`);
    return result;
  }

  subtract(a: number, b: number): number {
    const result = a - b;
    this.history.push(`${a} - ${b} = ${result}`);
    return result;
  }

  multiply(a: number, b: number): number {
    const result = a * b;
    this.history.push(`${a} * ${b} = ${result}`);
    return result;
  }

  divide(a: number, b: number): number {
    if (b === 0) {
      throw new Error('Cannot divide by zero');
    }
    const result = a / b;
    this.history.push(`${a} / ${b} = ${result}`);
    return result;
  }

  getHistory(): string[] {
    return this.history;
  }

  clearHistory(): void {
    this.history = [];
  }
}

describe('Calculator', () => {
  let calculator: Calculator;

  // Runs before each test
  beforeEach(() => {
    calculator = new Calculator();
  });

  // Runs after each test
  afterEach(() => {
    calculator.clearHistory();
  });

  describe('add', () => {
    test('should add two positive numbers', () => {
      expect(calculator.add(2, 3)).toBe(5);
    });

    test('should add negative numbers', () => {
      expect(calculator.add(-5, -3)).toBe(-8);
    });

    test('should add to history', () => {
      calculator.add(2, 3);
      expect(calculator.getHistory()).toContain('2 + 3 = 5');
    });
  });

  describe('subtract', () => {
    test('should subtract two numbers', () => {
      expect(calculator.subtract(10, 4)).toBe(6);
    });

    test('should handle negative results', () => {
      expect(calculator.subtract(3, 10)).toBe(-7);
    });
  });

  describe('multiply', () => {
    test('should multiply two numbers', () => {
      expect(calculator.multiply(4, 5)).toBe(20);
    });

    test('should return 0 when multiplying by 0', () => {
      expect(calculator.multiply(5, 0)).toBe(0);
    });
  });

  describe('divide', () => {
    test('should divide two numbers', () => {
      expect(calculator.divide(10, 2)).toBe(5);
    });

    test('should handle decimal results', () => {
      expect(calculator.divide(10, 3)).toBeCloseTo(3.333, 2);
    });

    test('should throw error when dividing by zero', () => {
      expect(() => calculator.divide(10, 0)).toThrow('Cannot divide by zero');
    });
  });

  describe('history', () => {
    test('should track multiple operations', () => {
      calculator.add(1, 2);
      calculator.multiply(3, 4);
      calculator.subtract(10, 5);

      const history = calculator.getHistory();
      expect(history).toHaveLength(3);
      expect(history[0]).toBe('1 + 2 = 3');
      expect(history[1]).toBe('3 * 4 = 12');
      expect(history[2]).toBe('10 - 5 = 5');
    });

    test('should clear history', () => {
      calculator.add(1, 2);
      calculator.clearHistory();
      expect(calculator.getHistory()).toHaveLength(0);
    });
  });
});
