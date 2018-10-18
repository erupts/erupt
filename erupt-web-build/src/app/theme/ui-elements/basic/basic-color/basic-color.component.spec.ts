import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { BasicColorComponent } from './basic-color.component';

describe('BasicColorComponent', () => {
  let component: BasicColorComponent;
  let fixture: ComponentFixture<BasicColorComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ BasicColorComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BasicColorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
