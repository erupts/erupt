import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { HeaderFooterRegComponent } from './header-footer-reg.component';

describe('HeaderFooterRegComponent', () => {
  let component: HeaderFooterRegComponent;
  let fixture: ComponentFixture<HeaderFooterRegComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ HeaderFooterRegComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(HeaderFooterRegComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
