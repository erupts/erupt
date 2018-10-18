import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { HeaderFooterLoginComponent } from './header-footer-login.component';

describe('HeaderFooterLoginComponent', () => {
  let component: HeaderFooterLoginComponent;
  let fixture: ComponentFixture<HeaderFooterLoginComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ HeaderFooterLoginComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(HeaderFooterLoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
