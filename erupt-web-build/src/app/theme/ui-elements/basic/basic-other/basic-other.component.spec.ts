import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { BasicOtherComponent } from './basic-other.component';

describe('BasicOtherComponent', () => {
  let component: BasicOtherComponent;
  let fixture: ComponentFixture<BasicOtherComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ BasicOtherComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BasicOtherComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
