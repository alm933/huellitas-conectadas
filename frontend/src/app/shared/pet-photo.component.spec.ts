import { TestBed } from '@angular/core/testing';
import { PetPhotoComponent } from './pet-photo.component';

describe('PetPhotoComponent', () => {
  it('shows paws for missing, empty and blank photo URLs', () => {
    const fixture = TestBed.createComponent(PetPhotoComponent);
    fixture.componentRef.setInput('name', 'Luna');
    for (const src of [null, '', '   ']) {
      fixture.componentRef.setInput('src', src);
      fixture.detectChanges();
      const element = fixture.nativeElement as HTMLElement;
      expect(element.querySelector('img')).toBeNull();
      expect(element.querySelector('[role="img"]')?.getAttribute('aria-label')).toContain('Luna');
      expect(element.textContent).toContain('🐾');
    }
  });

  it('replaces a broken image with paws and displays a new photo when the URL changes', () => {
    const fixture = TestBed.createComponent(PetPhotoComponent);
    fixture.componentRef.setInput('name', 'Jerry');
    fixture.componentRef.setInput('src', '/missing-photo.jpg');
    fixture.detectChanges();
    const element = fixture.nativeElement as HTMLElement;
    element.querySelector('img')!.dispatchEvent(new Event('error'));
    fixture.detectChanges();
    expect(element.querySelector('img')).toBeNull();
    expect(element.textContent).toContain('🐾');

    fixture.componentRef.setInput('src', '/api/v1/fotos/2');
    fixture.detectChanges();
    expect(element.querySelector('img')?.getAttribute('src')).toBe('/api/v1/fotos/2');
    expect(element.querySelector('img')?.alt).toBe('Foto de Jerry');
    expect(element.querySelector('.photo-placeholder')).toBeNull();
  });
});
