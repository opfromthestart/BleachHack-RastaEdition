/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.mixin;

import bleach.hack.module.ModuleManager;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleEffect;

@Mixin(ParticleManager.class)
public class MixinParticleManager {

	@Inject(method = "addParticle(Lnet/minecraft/client/particle/Particle;)V", at = @At("HEAD"), cancellable = true)
	public void addParticle(Particle particle, CallbackInfo callback) {
		// pls send help
		EventParticle.Normal event = new EventParticle.Normal(particle);
		BleachHack.eventBus.post(event);

		if (event.isCancelled()) {
			callback.cancel();
		}
	}

	@Inject(method = "addEmitter(Lnet/minecraft/entity/Entity;Lnet/minecraft/particle/ParticleEffect;)V", at = @At("HEAD"), cancellable = true)
	public void addEmitter(Entity entity, ParticleEffect particleEffect, CallbackInfo callback) {
		EventParticle.Emitter event = new EventParticle.Emitter(particleEffect);
		BleachHack.eventBus.post(event);

		if (event.isCancelled()) {
			callback.cancel();
		}
	}

	@Inject(method = "addEmitter(Lnet/minecraft/entity/Entity;Lnet/minecraft/particle/ParticleEffect;I)V", at = @At("HEAD"), cancellable = true)
	public void addEmitter_(Entity entity, ParticleEffect particleEffect, int maxAge, CallbackInfo callback) {
		EventParticle.Emitter event = new EventParticle.Emitter(particleEffect);
		BleachHack.eventBus.post(event);

		if (event.isCancelled()) {
			callback.cancel();
		}
	}
	@Inject(method = "addBlockBreakParticles", at = @At("HEAD"), cancellable = true)
	public void addBlockBreakParticles(BlockPos pos, BlockState state, CallbackInfo ci) {
		if (ModuleManager.getModule("Nuker").isEnabled() && ModuleManager.getModule("Nuker").getSetting(9).asToggle().state) {
			ci.cancel();
		}
		else if (ModuleManager.getModule("NoRender").isEnabled() && ModuleManager.getModule("NoRender").getSetting(1).asToggle().getChild(9).asToggle().state) {
			ci.cancel();
		}
	}
}