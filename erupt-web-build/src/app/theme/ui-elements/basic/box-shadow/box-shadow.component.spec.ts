import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { BoxShadowComponent } from './box-shadow.component';

describe('BoxShadowComponent', () => {
  let component: BoxShadowComponent;
  let fixture: ComponentFixture<BoxShadowComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ BoxShadowComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BoxShadowComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
