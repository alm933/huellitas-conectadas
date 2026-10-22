import { Component, input } from '@angular/core';

@Component({
  selector: 'app-status-notice',
  template: `
    @if (message()) {
      <p [class]="kind() === 'error' ? 'alert alert-danger' : 'alert alert-success'"
        [attr.role]="kind() === 'error' ? 'alert' : 'status'">{{ message() }}</p>
    }
  `
})
export class StatusNoticeComponent {
  readonly message = input.required<string>();
  readonly kind = input<'success' | 'error'>('success');
}
