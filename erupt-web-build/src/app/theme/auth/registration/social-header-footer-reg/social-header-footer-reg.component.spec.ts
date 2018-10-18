import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SocialHeaderFooterRegComponent } from './social-header-footer-reg.component';

describe('SocialHeaderFooterRegComponent', () => {
  let component: SocialHeaderFooterRegComponent;
  let fixture: ComponentFixture<SocialHeaderFooterRegComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SocialHeaderFooterRegComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SocialHeaderFooterRegComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
