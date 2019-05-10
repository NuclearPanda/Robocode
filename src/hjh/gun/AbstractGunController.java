package hjh.gun;

public abstract class AbstractGunController implements GunController {
    protected EnemyDataProvider provider;

    AbstractGunController(EnemyDataProvider provider) {
        this.provider = provider;
    }

}
