import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SocialHeaderFooterLoginComponent } from './social-header-footer-login.component';

describe('SocialHeaderFooterLoginComponent', () => {
  let component: SocialHeaderFooterLoginComponent;
  let fixture: ComponentFixture<SocialHeaderFooterLoginComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SocialHeaderFooterLoginComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SocialHeaderFooterLoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
