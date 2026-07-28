import { A11yModule } from '@angular/cdk/a11y';
import { ChangeDetectionStrategy, Component, signal } from '@angular/core';

@Component({
  selector: 'app-showcase',
  imports: [A11yModule],
  templateUrl: './showcase.html',
  styleUrl: './showcase.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Showcase {
  protected readonly drawerOpen = signal(false);
  protected readonly dialogOpen = signal(false);

  protected closeDrawer(): void { this.drawerOpen.set(false); }
  protected closeDialog(): void { this.dialogOpen.set(false); }
  protected openDrawer(): void { this.drawerOpen.set(true); }
  protected openDialog(): void { this.dialogOpen.set(true); }
}
