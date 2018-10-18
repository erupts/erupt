import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { GenericClassComponent } from './generic-class.component';

describe('GenericClassComponent', () => {
  let component: GenericClassComponent;
  let fixture: ComponentFixture<GenericClassComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ GenericClassComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(GenericClassComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
