import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { C3JsComponent } from './c3-js.component';

describe('C3JsComponent', () => {
  let component: C3JsComponent;
  let fixture: ComponentFixture<C3JsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ C3JsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(C3JsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
