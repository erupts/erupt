import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { BasicRegComponent } from './basic-reg.component';

describe('BasicRegComponent', () => {
  let component: BasicRegComponent;
  let fixture: ComponentFixture<BasicRegComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ BasicRegComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BasicRegComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
